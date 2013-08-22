package nl.surfnet.bod.domain;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class VlanRangesValidator implements ConstraintValidator<VlanRanges, String> {

  @Override
  public void initialize(VlanRanges constraintAnnotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    try {
      parseRanges(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static RangeSet<Integer> parseRanges(String vlanRanges) {
    ImmutableRangeSet.Builder<Integer> builder = ImmutableRangeSet.builder();
    for (String range: vlanRanges.split(",")) {
      String[] xs = range.split("-");
      if (xs.length == 1) {
        builder.add(Range.singleton(Integer.parseInt(xs[0].trim())));
      } else if (xs.length == 2) {
        int lower = Integer.parseInt(xs[0].trim());
        int upper = Integer.parseInt(xs[1].trim());
        if (lower > upper) {
          throw new IllegalArgumentException("lower bound " + lower + " cannot be greater than upper bound " + upper);
        }
        builder.add(Range.closed(lower, upper));
      } else {
        throw new IllegalArgumentException("invalid range " + vlanRanges);
      }
    }
    return builder.build();
  }
}
